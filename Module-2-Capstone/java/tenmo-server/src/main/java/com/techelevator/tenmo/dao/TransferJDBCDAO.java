package com.techelevator.tenmo.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.tenmo.model.Transfer;
//might want to add some defensive programming here 
public class TransferJDBCDAO implements TransferDAO {
	
	private JdbcTemplate jdbcTemplate;
	
	public TransferJDBCDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = new JdbcTemplate();
	}
	
	@Override
	public void createTransfer(Transfer newTransfer) {
		String sqlNewTransfer ="INSERT INTO transfers "
							  + "(transfer_id, transfer_type_id, transfer_status_id) "
							  + "account_from, account_to, amount) "	
							  +	"VALUES(?,?, ?, ?, ?, ?)";
		
		newTransfer.setTransfer_id(getNextTransferId());
		
		jdbcTemplate.update(sqlNewTransfer, newTransfer.getTransfer_id(), newTransfer.getTransfer_type_id(), newTransfer.getTransfer_status_id(),
								newTransfer.getAccount_from(), newTransfer.getAccount_to(), newTransfer.getAmount());
		
	}

	@Override
	public List<Transfer> listTransfersByAccount(int account_from) {
		List<Transfer> returnList = new ArrayList<Transfer>();
		
		String sqlListTransfers = "SELECT * " 
								+ "FROM transfers "
								+ "INNER JOIN transfer_statuses "
								+ "ON transfer_statuses.transfer_status_id = transfers.transfer_status_id "
								+ "INNER JOIN transfer_types "
								+ "ON transfer_types.transfer_type_id = transfers.transfer_type_id "
								+ "WHERE account_from = ? or account_to = ? ";
		
		SqlRowSet transferQuery = jdbcTemplate.queryForRowSet(sqlListTransfers + account_from + account_from);
		
		while(transferQuery.next()) {
			Transfer theTransfer =  mapRowToTransfer(transferQuery);
			returnList.add(theTransfer);
		}
		
		return returnList;
	}

	@Override
	public List<Transfer> listTransfersById(Long transfer_id) {
		List<Transfer> returnList = new ArrayList<Transfer>();
		
		String sqlListTransfers = "SELECT * " 
								+ "FROM transfers "
								+ "INNER JOIN transfer_statuses "
								+ "ON transfer_statuses.transfer_status_id = transfers.transfer_status_id "
								+ "INNER JOIN transfer_types "
								+ "ON transfer_types.transfer_type_id = transfers.transfer_type_id "
								+ "WHERE transfer_id = ? ";
		
		SqlRowSet transferQuery = jdbcTemplate.queryForRowSet(sqlListTransfers + transfer_id);
		
		while(transferQuery.next()) {
			Transfer theTransfer =  mapRowToTransfer(transferQuery);
			returnList.add(theTransfer);
		}
		
		return returnList;
	}

	@Override
	public void updateTransferStatus(Long transfer_id, int transfer_status_id) {
		String sqlUpdateTransfer = "UPDATE transfers "
								+ "SET transfer_status_id = ? "
								+ "WHERE transfer_id = ? ";	
		
		jdbcTemplate.update(sqlUpdateTransfer, transfer_status_id, transfer_id);

	}
	
	private Transfer mapRowToTransfer(SqlRowSet results) {
		Transfer theTransfer = new Transfer();
	
		theTransfer.setTransfer_id(results.getLong("transfer_id")); 
		
		theTransfer.setTransfer_type_id(results.getInt("transfer_type_id"));
		
		theTransfer.setTransfer_status_id(results.getInt("status_id"));
		
		theTransfer.setAccount_from(results.getInt("account_from"));
		
		theTransfer.setAccount_to(results.getInt("account_to"));
		
		theTransfer.setAmount(results.getDouble("amount"));
		
		theTransfer.setTransfer_type(results.getString("transfer_type_desc"));
		
		theTransfer.setTransfer_status(results.getString("transfer_status_desc"));
		
		return theTransfer;
	}
			
	private long getNextTransferId() {
		SqlRowSet nextTransferIdResult = jdbcTemplate.queryForRowSet("SELECT nextval('seq_transfer_id')");
		
		if(nextTransferIdResult.next()) {
			return nextTransferIdResult.getLong(1);
		}else {
			throw new RuntimeException ("Something went wrong while getting an id for the new transfer");
		}
	}

}