package br.com.fiap.petcare360_java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.petcare360_java.model.PetMessage;

public interface PetMessageRepository extends JpaRepository<PetMessage, Long> {
	List<PetMessage> findBySenderEmailOrReceiverEmailOrderByCreatedAtDesc(String senderEmail, String receiverEmail);

	@Query("""
			select message from PetMessage message
			join fetch message.pet pet
			join fetch message.sender sender
			join fetch message.receiver receiver
			where lower(sender.email) = lower(:email)
				or lower(receiver.email) = lower(:email)
			order by message.createdAt desc
			""")
	List<PetMessage> findVisibleForUser(@Param("email") String email);

	@Query("""
			select message from PetMessage message
			join fetch message.pet pet
			join fetch message.sender sender
			join fetch message.receiver receiver
			order by message.createdAt desc
			""")
	List<PetMessage> findAllVisible();
}
