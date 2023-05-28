package dk.reimer.claus;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="transactions")
public class Transaction {
    @Id
    @Column(name = "id")
    private String id;

    @Column (name = "date")
    private Date date;

    @AccountNumber
    @Column (name = "account_number")
    private String accountNumber;

    @NotNull
    @Column (name = "amount")
    private BigDecimal amount;

    public Transaction() {
    }

    public Transaction(String id, Date date, String accountNumber, BigDecimal amount) {
        this.id = id;
        this.date = date;
        this.accountNumber = accountNumber;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", date=" + date +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id) && Objects.equals(date, that.date) && Objects.equals(accountNumber, that.accountNumber) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
