
public class TransactionInput {
	public String transactionOutputID;
	public TransactionOutput unspentTX;
	
	public TransactionInput(String transactionOutputID) {
		
		this.transactionOutputID = transactionOutputID;
	}
}
