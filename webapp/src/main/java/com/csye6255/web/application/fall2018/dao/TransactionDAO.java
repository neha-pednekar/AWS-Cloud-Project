package com.csye6255.web.application.fall2018.dao;

import java.util.List;
import com.csye6255.web.application.fall2018.pojo.Transaction;
import org.springframework.data.repository.CrudRepository;


public interface  TransactionDAO extends CrudRepository<Transaction, Long> {

    List<Transaction> findByUserId(long userId);
    List<Transaction> findByTransactionid(String transactionid);
}
