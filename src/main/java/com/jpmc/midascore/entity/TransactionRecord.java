package com.jpmc.midascore.entity;

// import org.h2.engine.User;

import jakarta.persistence.*;


@Entity
public class TransactionRecord {
    @Id
    @GeneratedValue
    private long id;

    private float amount;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserRecord sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserRecord recipient;

    public TransactionRecord(){
    }

    public TransactionRecord(float amount, UserRecord sender, UserRecord recipient) {
        this.amount = amount;
        this.sender = sender;
        this.recipient = recipient;
    }

    public long getId(){
        return id;
    }

    public float getAmount(){
        return amount;
    }

    public UserRecord getSender(){
        return sender;
    }

    public UserRecord getRecipient(){
        return recipient;
    }

    public void setAmount(float amount){
        this.amount = amount;
    }

    public void setSender(UserRecord sender){
        this.sender = sender;
    }

    public void setRecipient(UserRecord recipient){
        this.recipient = recipient;   
    }

    @Override
    public String toString() {
        return String.format("Transaction[id=%d, sender=%s, recipient=%s, amount=%f, incentive=%f]",
            id, sender.getName(), recipient.getName(), amount, incentive);
    }


    @Column
    private float incentive;
    public float getIncentive() { return incentive; }
    public void setIncentive(float incentive) { this.incentive = incentive; }

}
