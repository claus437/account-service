package dk.reimer.claus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, String> {
    List<Transaction> findAllByAccountNumber(String accountNumber, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "insert into transactions (id, date, account_number, amount) values (:#{#t.id},:#{#t.date},:#{#t.accountNumber},:#{#t.amount})", nativeQuery = true)
    void insert(@Param("t") Transaction transaction);
}
