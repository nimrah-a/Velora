# Velora — Digital Finance, Reimagined.

**A matte-black cryptocurrency wallet and blockchain laboratory built with Java 17 + Swing.**

Velora is a *fully functioning local simulation*: it starts with spendable demo asset balances, lets you issue more demo funds, transfer amounts between wallets, verify real Ed25519 signatures, and inspect SHA-256-linked blocks. **No real money or cryptocurrency is transferred.** There is no connection to Bitcoin, Ethereum, Solana, or any live exchange. The app never asks for real wallet keys.

## A completely redesigned interface

- **Matte black** background, **light blue** accents, **deep blue** navigation, and **white** typography.
- Sidebar navigation, portfolio hero, illustrative trend graphic (clearly labeled), clickable asset cards, and an interactive transfer workflow.
- **Funded default wallet:** Avery begins with 2.5 demo BTC, 12 demo ETH, and 120 demo SOL.
- **Add demo funds:** Use `+ Add demo funds` or `Wallets → + Add funds` to claim +0.5 BTC, +2 ETH, or +20 SOL for your current wallet, including newly created wallets. A reserve wallet funds the transfer, and it is recorded as a signed, confirmed transaction.
- **Actual working balances within the simulator:** transfer from one wallet to another, preview spendable balance, use 25%, 50% or MAX shortcuts, review the transaction and confirm; the recipient is credited and the sender is debited immediately after the signed transfer is mined locally.
- Block explorer, transaction inspection, cryptographic audit, and non-destructive tamper demonstration.

## Walkthrough

1. Launch the app. From **Home**, select Avery. You will see funded demo balances.
2. Click **Send assets**, choose Avery as sender and Jordan as recipient.
3. Choose **BTC**, click **25%** or enter `0.25`, then **Review & complete transfer**.
4. Confirm the signed transaction. The app mines a local block and immediately updates both wallets.
5. Switch to Jordan on **Home** to see the new incoming funds.
6. Create a new wallet on **Wallets**, select it, and click **+ Add funds**. You can then send its spendable demo balances.
7. Open **Activity**, **Blockchain**, and **Security** to inspect the cryptographic details.

## Technical implementation

- Java 17, Swing, `BigDecimal`, immutable Java records, collections and security APIs.
- Genuine Ed25519 digital signatures on every normal transfer; SHA-256 block hashes and simple educational proof-of-work (hash prefix `00`).
- Available-balance validation, duplicate-transaction detection, chain integrity verification, and double-spend prevention.
- Internal treasury-backed demo funding; no fabricated live prices or USD valuation.
- In-memory only: restarting the application resets demo wallets and balances. **Not a production wallet or blockchain.** Asset symbols refer to fictional units on the local ledger, and their signatures are not Bitcoin/Ethereum protocol compatible.

## Run

**Requirements:** JDK 17+ and a desktop environment. No external Java libraries, Maven, API keys, or internet needed.

**macOS / Linux**

```bash
bash run.sh
```

**Windows:** execute `run.bat`.

**IntelliJ IDEA:** open the `Velora-Java` folder, set Java SDK 17+, and run `src/velora/Main.java`.

**Tests:** `bash test.sh` (or `test.bat` on Windows). CI is configured under `.github/workflows/java.yml`.

## Source

- `src/velora/Main.java` — GUI launcher
- `src/velora/WalletStudio.java` — redesigned Swing desktop interface
- `src/velora/CryptoLedger.java` — wallets, transfers, signatures, mining, verification
- `tests/velora/LedgerTests.java` — repeatable tests of funding, settlement, and ledger security

### Real asset transfers?

Not included. A genuine crypto wallet requires a separate and much more security-sensitive design: network-specific address formats and signing conventions, blockchain RPC connectivity, secure key management, transaction fees, replay protection and comprehensive auditing. Velora does **not** pretend its demo token units are real cryptocurrency.
