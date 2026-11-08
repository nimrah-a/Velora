package velora;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class LedgerTests {
    private static int checks;
    private static void ok(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    private static void throwsError(Runnable run, String message) {
        checks++; try { run.run(); throw new AssertionError(message); } catch (IllegalArgumentException | IllegalStateException expected) { }
    }
    public static void main(String[] args) {
        CryptoLedger ledger = new CryptoLedger();
        String a = ledger.wallets().get(0).address(), b = ledger.wallets().get(1).address();
        ok(ledger.audit().valid(), "Initial blockchain audit");
        ok(ledger.confirmedBalance(a, "BTC").compareTo(new BigDecimal("2.5")) == 0, "Initial allocation");
        CryptoLedger.Transfer tx = ledger.send(a, b, "BTC", new BigDecimal("1.25"));
        ok(CryptoLedger.verifySignature(tx), "Correct signature validates");
        ok(ledger.confirmedBalance(b, "BTC").signum() == 0, "Pending transfers not confirmed");
        ok(ledger.availableBalance(a, "BTC").compareTo(new BigDecimal("1.25")) == 0, "Pending transfer reserves funds");
        throwsError(() -> ledger.send(a,b,"BTC", new BigDecimal("1.26")), "Overspend rejected");
        throwsError(() -> ledger.send(a,b,"BTC", new BigDecimal("-1")), "Negative amount rejected");
        throwsError(() -> ledger.send(a,b,"BTC", new BigDecimal("0.00001")), "Excess precision rejected");
        throwsError(() -> ledger.send(a,a,"BTC", new BigDecimal("1")), "Self transfer rejected");
        CryptoLedger.Transfer tampered = new CryptoLedger.Transfer(tx.id(), tx.sender(), tx.recipient(), tx.asset(), new BigDecimal("1.3"), tx.timestamp(), tx.nonce(), tx.publicKeyBase64(), tx.signatureBase64());
        ok(!CryptoLedger.verifySignature(tampered), "Modified signed payload rejected");
        CryptoLedger.Block block = ledger.minePending();
        ok(block.index() == 1 && block.hash().startsWith("00"), "Proof-of-work");
        ok(ledger.pending().isEmpty(), "Mempool cleared");
        ok(ledger.confirmedBalance(b, "BTC").compareTo(new BigDecimal("1.25")) == 0, "Recipient credited");
        ok(ledger.audit().valid(), "Chain verified after mine");
        List<CryptoLedger.Block> altered = new ArrayList<>(ledger.blocks());
        CryptoLedger.Block original = altered.get(0);
        altered.set(0, new CryptoLedger.Block(original.index(), original.timestamp(), original.previousHash(), original.transfers(), original.nonce()+1, original.hash()));
        ok(!CryptoLedger.auditBlocks(altered).valid(), "Tampering detected");
        ledger.send(b, a, "BTC", new BigDecimal("0.25")); ledger.minePending();
        ok(ledger.audit().valid(), "Multiple blocks and spends verify");
        ok(ledger.confirmedBalance(a, "BTC").compareTo(new BigDecimal("1.50")) == 0, "Round-trip balance");
        CryptoLedger.Wallet fresh = ledger.createWallet("Demo New Wallet");
        ok(ledger.availableBalance(fresh.address(), "ETH").signum() == 0, "New wallet begins empty");
        CryptoLedger.Transfer funded = ledger.claimDemoFunds(fresh.address(), "ETH");
        ok(CryptoLedger.verifySignature(funded), "Treasury funding is digitally signed");
        ok(ledger.availableBalance(fresh.address(), "ETH").compareTo(new BigDecimal("2")) == 0, "Demo funding is spendable");
        CryptoLedger.Transfer move = ledger.send(fresh.address(), b, "ETH", new BigDecimal("1.5"));
        ok(CryptoLedger.verifySignature(move), "Funded wallet signs transfer");
        ok(ledger.availableBalance(fresh.address(), "ETH").compareTo(new BigDecimal("0.5")) == 0, "Sending deducts available funds");
        ledger.minePending();
        ok(ledger.confirmedBalance(b, "ETH").compareTo(new BigDecimal("1.5")) == 0, "Recipient receives funded assets");
        ok(ledger.audit().valid(), "Funded transfers keep ledger valid");
        System.out.println("PASS: " + checks + " ledger assertions, including crypto signatures, mining, double-spend prevention and tamper detection.");
    }
}
