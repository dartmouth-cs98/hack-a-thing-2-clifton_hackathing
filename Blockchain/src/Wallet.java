import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	public HashMap<String,TransactionOutput> unspentTransactions = new HashMap<String,TransactionOutput>();
	
	
	
	public Wallet() {
		generateKeys();
	}
	
	private void generateKeys() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec specs = new ECGenParameterSpec("prime192v1");
			
			generator.initialize(specs, random);
	        KeyPair keyPair = generator.generateKeyPair();
	        	        
	        privateKey = keyPair.getPrivate();
	        
	        publicKey = keyPair.getPublic();
	        
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public float getBalance() {
		float total = 0;	
        for (Map.Entry<String, TransactionOutput> item: Blockchain.unspentTransactions.entrySet()){
        	TransactionOutput unspentTX = item.getValue();
            if(unspentTX.isMine(publicKey)) {
            	unspentTransactions.put(unspentTX.id,unspentTX); 
            	total += unspentTX.value ; 
            }
        }
        
		return total;
	}
	
	public Transaction sendFunds(PublicKey recipient, float value) {
		
		if(getBalance() < value) {
			System.out.println("Not enough funds for this transaction");
			return(null);
		}
		
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

		float total = 0;
		
		for(Map.Entry<String, TransactionOutput> item: unspentTransactions.entrySet()) {
			TransactionOutput unspentTX = item.getValue();
			total += unspentTX.value;
			inputs.add(new TransactionInput(unspentTX.id));
			if(total > value) break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
		
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput i: inputs) {
			unspentTransactions.remove(i.transactionOutputID);
		}
		
		return(newTransaction);
	}
	
}
