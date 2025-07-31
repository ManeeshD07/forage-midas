package com.jpmc.midascore.listener;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaTransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTransactionListener.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-group")
    @Transactional
    public void listen(Transaction transaction) {
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender == null || recipient == null) {
            logger.warn("Invalid sender or recipient for transaction: {}", transaction);
            return;
        }

        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("Insufficient balance for sender '{}', skipping transaction of amount {}.",
                    sender.getName(), transaction.getAmount());
            return;
        }
        

        // Update balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());

        userRepository.save(sender);
        userRepository.save(recipient);

        // Record transaction
        TransactionRecord record = new TransactionRecord(transaction.getAmount(), sender, recipient);
        transactionRepository.save(record);

        logger.info("Transaction recorded: {}", record);

        // Log Waldorf balance if involved
        if ("waldorf".equalsIgnoreCase(sender.getName()) || "waldorf".equalsIgnoreCase(recipient.getName())) {
            UserRecord waldorf = userRepository.findByName("waldorf");
            logger.info("Waldorf’s updated balance: {}", (int) Math.floor(waldorf.getBalance()));
        }

        // Log Wilbur balance if involved
        if ("wilbur".equalsIgnoreCase(sender.getName()) || "wilbur".equalsIgnoreCase(recipient.getName())) {
            UserRecord wilbur = userRepository.findByName("wilbur");
            logger.info("Wilbur’s updated balance: {}", (int) Math.floor(wilbur.getBalance()));
        }
    }
}
