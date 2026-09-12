package br.com.fiap.petcare360_java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.petcare360_java.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
	List<Appointment> findByTutorEmailOrVeterinarianEmailOrderByScheduledAtDesc(String tutorEmail, String veterinarianEmail);

	@Query("""
			select appointment from Appointment appointment
			join fetch appointment.pet pet
			join fetch appointment.tutor tutor
			join fetch appointment.veterinarian veterinarian
			where lower(tutor.email) = lower(:email)
				or lower(veterinarian.email) = lower(:email)
			order by appointment.scheduledAt desc
			""")
	List<Appointment> findVisibleForUser(@Param("email") String email);

	@Query("""
			select appointment from Appointment appointment
			join fetch appointment.pet pet
			join fetch appointment.tutor tutor
			join fetch appointment.veterinarian veterinarian
			order by appointment.scheduledAt desc
			""")
	List<Appointment> findAllVisible();
}
