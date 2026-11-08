package velora;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

/** In-memory teaching ledger. This is NOT a Bitcoin, Ethereum, or Solana implementation. */
public final class CryptoLedger {
    public static final List<String> ASSETS = List.of("BTC", "ETH", "SOL");
    public static final int DIFFICULTY = 2;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final String GENESIS_FROM = "GENESIS";

    public record Wallet(String name, String address, PublicKey publicKey, PrivateKey privateKey) { }
    public record Transfer(String id, String sender, String recipient, String asset, BigDecimal amount,
                           long timestamp, long nonce, String publicKeyBase64, String signatureBase64) {
        public String payload() {
            return sender + "|" + recipient + "|" + asset + "|" + amount.toPlainString() + "|" + timestamp + "|" + nonce;
        }
    }
    public record Block(int index, long timestamp, String previousHash, List<Transfer> transfers,
                        long nonce, String hash) { }
    public record Audit(boolean valid, String message) { }

    private final LinkedHashMap<String, Wallet> wallets = new LinkedHashMap<>();
    private final List<Block> chain = new ArrayList<>();
    private final List<Transfer> pending = new ArrayList<>();
    private long txNonce = 0;
    private final String reserveAddress;

    public CryptoLedger() {
        createWallet("Avery");
        createWallet("Jordan");
        createWallet("Taylor");
        reserveAddress = createWallet("Demo Reserve").address();
        Wallet a = wallets.values().iterator().next();
        List<Transfer> initial = List.of(
            genesis(a.address(), "BTC", "2.5000"),
            genesis(a.address(), "ETH", "12.0000"),
            genesis(a.address(), "SOL", "120.0000"),
            genesis(reserveAddress, "BTC", "100.0000"),
            genesis(reserveAddress, "ETH", "500.0000"),
            genesis(reserveAddress, "SOL", "2000.0000")
        );
        chain.add(mineBlock(0, "GENESIS", initial));
    }

    public synchronized Wallet createWallet(String name) {
        String normalized = Objects.requireNonNull(name).trim();
        if (normalized.isEmpty() || normalized.length() > 32) throw new IllegalArgumentException("Wallet name must contain 1–32 characters.");
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair pair = gen.generateKeyPair();
            String address = "VL" + sha256Hex(pair.getPublic().getEncoded()).substring(0, 24).toUpperCase(Locale.ROOT);
            Wallet wallet = new Wallet(normalized, address, pair.getPublic(), pair.getPrivate());
            wallets.put(address, wallet);
            return wallet;
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Ed25519 unavailable", e); }
    }

    private Transfer genesis(String recipient, String asset, String amount) {
        long nonce = ++txNonce;
        String payload = GENESIS_FROM + "|" + recipient + "|" + asset + "|" + amount + "|0|" + nonce;
        return new Transfer(sha256Hex(payload), GENESIS_FROM, recipient, asset, new BigDecimal(amount), 0L, nonce, "", "");
    }

    /** Developer-controlled demo dispenser: signs a real simulated transfer from the seeded reserve. */
    public synchronized Transfer claimDemoFunds(String recipient, String asset) {
        if (recipient == null || recipient.equals(reserveAddress) || !wallets.containsKey(recipient))
            throw new IllegalArgumentException("Select a regular demo wallet to receive funds.");
        String quantity = switch (asset) {
            case "BTC" -> "0.5";
            case "ETH" -> "2";
            case "SOL" -> "20";
            default -> throw new IllegalArgumentException("Choose BTC, ETH or SOL.");
        };
        Transfer tx = send(reserveAddress, recipient, asset, new BigDecimal(quantity));
        minePending();
        return tx;
    }

    public String reserveAddress() { return reserveAddress; }

    public synchronized List<Wallet> wallets() { return List.copyOf(wallets.values()); }
    public synchronized List<Block> blocks() { return List.copyOf(chain); }
    public synchronized List<Transfer> pending() { return List.copyOf(pending); }
    public synchronized int confirmedTransactionCount() { return chain.stream().mapToInt(b -> b.transfers().size()).sum(); }

    public synchronized BigDecimal confirmedBalance(String address, String asset) {
        BigDecimal amount = ZERO;
        for (Block block : chain) {
            for (Transfer tx : block.transfers()) {
                if (!tx.asset().equals(asset)) continue;
                if (tx.recipient().equals(address)) amount = amount.add(tx.amount());
                if (tx.sender().equals(address)) amount = amount.subtract(tx.amount());
            }
        }
        return amount;
    }
    public synchronized BigDecimal availableBalance(String address, String asset) {
        BigDecimal amount = confirmedBalance(address, asset);
        for (Transfer tx : pending) {
            if (tx.asset().equals(asset)) {
                if (tx.sender().equals(address)) amount = amount.subtract(tx.amount());
            }
        }
        return amount;
    }

    public synchronized Transfer send(String from, String to, String asset, BigDecimal amount) {
        if (!wallets.containsKey(from)) throw new IllegalArgumentException("Select a wallet you own.");
        if (!wallets.containsKey(to)) throw new IllegalArgumentException("The recipient address is not in this demo.");
        if (from.equals(to)) throw new IllegalArgumentException("Choose a different recipient wallet.");
        if (!ASSETS.contains(asset)) throw new IllegalArgumentException("Unsupported simulated asset.");
        if (amount == null || amount.signum() <= 0 || amount.scale() > 4 || amount.compareTo(new BigDecimal("1000000000")) > 0)
            throw new IllegalArgumentException("Amount must be positive, at most four decimals, and below one billion.");
        if (availableBalance(from, asset).compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient available balance.");
        long stamp = Instant.now().toEpochMilli();
        long nonce = ++txNonce;
        Wallet wallet = wallets.get(from);
        String publicKey = Base64.getEncoder().encodeToString(wallet.publicKey().getEncoded());
        String canonicalAmount = amount.stripTrailingZeros().toPlainString();
        String payload = from + "|" + to + "|" + asset + "|" + canonicalAmount + "|" + stamp + "|" + nonce;
        try {
            Signature signer = Signature.getInstance("Ed25519");
            signer.initSign(wallet.privateKey());
            signer.update(payload.getBytes(StandardCharsets.UTF_8));
            String sig = Base64.getEncoder().encodeToString(signer.sign());
            Transfer tx = new Transfer(sha256Hex(payload + "|" + publicKey + "|" + sig), from, to, asset,
                    new BigDecimal(canonicalAmount), stamp, nonce, publicKey, sig);
            if (!verifySignature(tx)) throw new IllegalStateException("Unable to verify locally signed transaction.");
            pending.add(tx);
            return tx;
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Signing failed", e); }
    }

    public static boolean verifySignature(Transfer tx) {
        if (tx.sender().equals(GENESIS_FROM)) return false;
        try {
            byte[] bytes = Base64.getDecoder().decode(tx.publicKeyBase64());
            PublicKey key = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(bytes));
            String derived = "VL" + sha256Hex(bytes).substring(0, 24).toUpperCase(Locale.ROOT);
            if (!derived.equals(tx.sender())) return false;
            if (!sha256Hex(tx.payload() + "|" + tx.publicKeyBase64() + "|" + tx.signatureBase64()).equals(tx.id())) return false;
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(key);
            verifier.update(tx.payload().getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(tx.signatureBase64()));
        } catch (GeneralSecurityException | IllegalArgumentException e) { return false; }
    }

    /** Mining is intentionally lightweight, for interactive demos (two hex zeros). */
    public synchronized Block minePending() {
        if (pending.isEmpty()) throw new IllegalStateException("There are no pending transfers to mine.");
        Block next = mineBlock(chain.size(), chain.get(chain.size() - 1).hash(), List.copyOf(pending));
        chain.add(next);
        pending.clear();
        return next;
    }

    private static Block mineBlock(int index, String previousHash, List<Transfer> transfers) {
        long stamp = Instant.now().toEpochMilli();
        long nonce = 0;
        String hash;
        do {
            hash = calculateBlockHash(index, stamp, previousHash, transfers, nonce++);
        } while (!hash.startsWith("0".repeat(DIFFICULTY)));
        return new Block(index, stamp, previousHash, List.copyOf(transfers), nonce - 1, hash);
    }
    public static String calculateBlockHash(int index, long timestamp, String previousHash, List<Transfer> transactions, long nonce) {
        StringBuilder b = new StringBuilder(index + "|" + timestamp + "|" + previousHash + "|" + nonce);
        for (Transfer tx : transactions) b.append('|').append(tx.id());
        return sha256Hex(b.toString());
    }

    public synchronized Audit audit() { return auditBlocks(chain); }
    public static Audit auditBlocks(List<Block> blocks) {
        if (blocks.isEmpty()) return new Audit(false, "Missing genesis block.");
        Map<String, Map<String, BigDecimal>> balances = new HashMap<>();
        Set<String> seen = new HashSet<>();
        String expectedPrevious = "GENESIS";
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (block.index() != i || !block.previousHash().equals(expectedPrevious))
                return new Audit(false, "Broken block link at height " + i);
            String recomputed = calculateBlockHash(block.index(), block.timestamp(), block.previousHash(), block.transfers(), block.nonce());
            if (!recomputed.equals(block.hash()) || !block.hash().startsWith("0".repeat(DIFFICULTY)))
                return new Audit(false, "Block hash or proof-of-work invalid at height " + i);
            for (Transfer tx : block.transfers()) {
                if (!seen.add(tx.id())) return new Audit(false, "Duplicate transaction detected.");
                if (!ASSETS.contains(tx.asset()) || tx.amount().signum() <= 0 || tx.amount().scale() > 4)
                    return new Audit(false, "Invalid asset or amount.");
                if (tx.sender().equals(GENESIS_FROM)) {
                    if (i != 0 || tx.timestamp() != 0 || !tx.publicKeyBase64().isEmpty() || !tx.signatureBase64().isEmpty() ||
                            !sha256Hex(tx.payload()).equals(tx.id()))
                        return new Audit(false, "Invalid genesis allocation.");
                } else {
                    if (i == 0 || !verifySignature(tx)) return new Audit(false, "Invalid digital signature.");
                    BigDecimal before = balances.getOrDefault(tx.sender(), Map.of()).getOrDefault(tx.asset(), ZERO);
                    if (before.compareTo(tx.amount()) < 0) return new Audit(false, "Overspend detected.");
                    balances.computeIfAbsent(tx.sender(), k -> new HashMap<>()).put(tx.asset(), before.subtract(tx.amount()));
                }
                Map<String, BigDecimal> recipient = balances.computeIfAbsent(tx.recipient(), k -> new HashMap<>());
                recipient.put(tx.asset(), recipient.getOrDefault(tx.asset(), ZERO).add(tx.amount()));
            }
            expectedPrevious = block.hash();
        }
        return new Audit(true, "All " + blocks.size() + " blocks verified: links, hashes, signatures, and balances.");
    }

    public static String sha256Hex(String data) { return sha256Hex(data.getBytes(StandardCharsets.UTF_8)); }
    public static String sha256Hex(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
