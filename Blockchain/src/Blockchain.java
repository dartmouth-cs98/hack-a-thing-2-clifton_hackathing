import java.util.ArrayList;
import java.util.HashMap;

public class Blockchain {

	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String, TransactionOutput> unspentTransactions = new HashMap<String, TransactionOutput>();

	public static int difficulty = 2;
	public static String testString = new String(new char[difficulty]).replace('\0', '0');
	public static Wallet wallet1;
	public static Wallet wallet2;
	public static int minimumTransaction = 1;
	public static Transaction firstTransaction;

	public static boolean isChainValid() {
		Block currBlock = null;
		Block prevBlock = null;

		//store and pull unspent transactions while working through the ledger
		HashMap<String, TransactionOutput> tempUnspent = new HashMap<String, TransactionOutput>();
		tempUnspent.put(firstTransaction.outputs.get(0).id, firstTransaction.outputs.get(0));

		//For each block, starting on the second one
		for (int i = 1; i < blockchain.size(); i++) {
			
			currBlock = blockchain.get(i);
			prevBlock = blockchain.get(i - 1);

			if (!currBlock.hash.equals(currBlock.calcHash())) {
				System.out.println("Stored hash of Block " + i + " is different from calculated hash.");
				return false;
			}

			if (!prevBlock.hash.equals(currBlock.previousHash)) {
				System.out.println("Block " + i + " is disconnected from the previous block");
				return false;
			}
			
			if(!currBlock.hash.substring(0, difficulty).equals(testString)) {
				System.out.println("Block " + i + " hasn't been mined");
				return false;
			}
		
			//iterate through the transactions in the blocks ledger
			TransactionOutput tempOutput;
			for (int t = 0; t < currBlock.transactions.size(); t++) {
				Transaction currTransaction = currBlock.transactions.get(t);
	
				//
				if (!currTransaction.verifySignature()) {
					System.out.println("Signature on transaction " + t + " is invalid");
					return false;
				}
				if (currTransaction.getInputsValue() != currTransaction.getOutputsValue()) {
					System.out.println("Inputs are not equal to outputs on transaction " + t);
					return false;
				}
	
				//for each input into the transaction check if output has been made
				for (TransactionInput input : currTransaction.inputs) {
					
					tempOutput = tempUnspent.get(input.transactionOutputID);
					
					if (tempOutput == null) {
						System.out.println("Referenced input on transaction " + t + " is missing");
						return false;
					}
	
					if (input.unspentTX.value != tempOutput.value) {
						System.out.println("Referenced input on transaction " + t + " value is invalid");
						return false;
					}
					tempUnspent.remove(input.transactionOutputID);
				}
	
				//add each output from the transaction to temporary unspent
				for (TransactionOutput output : currTransaction.outputs) {
					tempUnspent.put(output.id, output);
				}
	
				if (currTransaction.outputs.get(0).recipient != currTransaction.recipient) {
					System.out.println("#Transaction " + t + " output recipient is not who it should be");
					return false;
				}
				if (currTransaction.outputs.get(1).recipient != currTransaction.sender) {
					System.out.println("#Transaction " + t + " output 'change' is not sender.");
					return false;
				}
	
			}
		}

		System.out.println("\nEverything is Valid");
		return (true);

	}

	public static void main(String[] args) {

		//make some wallets
		wallet1 = new Wallet();
		wallet2 = new Wallet();
		Wallet bank = new Wallet();

		//add initial coins to the system
		firstTransaction = new Transaction(bank.publicKey, wallet1.publicKey, 100, null);
		firstTransaction.generateSignature(bank.privateKey);
		firstTransaction.transactionID = StringUtil.startID;
		firstTransaction.outputs.add(new TransactionOutput(firstTransaction.recipient, firstTransaction.value,
				firstTransaction.transactionID));
		unspentTransactions.put(firstTransaction.outputs.get(0).id, firstTransaction.outputs.get(0));

		//mine first block and add the transaction to the block
		System.out.println("Creating first Block");
		Block firstBlock = new Block(StringUtil.startID);
		firstBlock.addTransaction(firstTransaction);
		firstBlock.mineBlock();
		blockchain.add(firstBlock);

		Block block2 = new Block(firstBlock.hash);
		System.out.println("\nWallet1's balance is: " + wallet1.getBalance());

		//transfer 40 to wallet1 from wallet1
		block2.addTransaction(wallet1.sendFunds(wallet2.publicKey, 40));
		block2.mineBlock();
		blockchain.add(block2);

		System.out.println("\nWallet1's balance is: " + wallet1.getBalance());
		System.out.println("Wallet2's balance is: " + wallet2.getBalance());

		//transfer too much, show error
		Block block3 = new Block(block2.hash);
		block3.addTransaction(wallet1.sendFunds(wallet2.publicKey, 100));
		block3.mineBlock();
		blockchain.add(block3);

		System.out.println("\nWallet1's balance is: " + wallet1.getBalance());
		System.out.println("Wallet2's balance is: " + wallet2.getBalance());

		//transfer back to wallet1
		Block block4 = new Block(block3.hash);
		block4.addTransaction(wallet2.sendFunds(wallet1.publicKey, 30));
		block4.mineBlock();
		blockchain.add(block4);

		System.out.println("\nWallet1's balance is: " + wallet1.getBalance());
		System.out.println("Wallet2's balance is: " + wallet2.getBalance());

		isChainValid();
	}
}
