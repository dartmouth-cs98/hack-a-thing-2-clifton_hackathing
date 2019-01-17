import java.util.ArrayList;
import java.util.Date;

public class Block {
	
	public String hash;
	public String previousHash;
	public String merkleRoot;
	
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	public long timestamp;
	public int nonce = 0;
	
	public Block(String prevHash) {
		
		this.previousHash = prevHash;
		this.timestamp = new Date().getTime();
		
		this.hash = calcHash();
		
	}
	
	public void mineBlock() {
		
		String tempHash = calcHash();
		
		
		while(!tempHash.substring(0, Blockchain.difficulty).equals(Blockchain.testString)) {
			nonce++;
			tempHash = calcHash();
		}

		hash = calcHash();
	}
	
	public String calcHash() {
		
		return(StringUtil.applySha256(this.previousHash + 
				Long.toString(timestamp) + Integer.toString(nonce) + merkleRoot));
	}
	
	public boolean addTransaction(Transaction transaction) {
		
		if(transaction == null) {
			
			return false;
		}
		if(previousHash != StringUtil.startID) {
			if(transaction.processTransaction() != true) {
				System.out.println("Transaction failed to process");
				return false;
			}
		}
		
		transactions.add(transaction);
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		hash = calcHash();
		
		System.out.println("Transaction added to Block");
		
		return true;
	}
}
