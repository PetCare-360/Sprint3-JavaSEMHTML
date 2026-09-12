package br.com.fiap.petcare360_java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.petcare360_java.model.CareRecommendation;

public interface CareRecommendationRepository extends JpaRepository<CareRecommendation, Long> {
	List<CareRecommendation> findByTutorEmailOrVeterinarianEmailOrderByCreatedAtDesc(String tutorEmail, String veterinarianEmail);

	@Query("""
			select recommendation from CareRecommendation recommendation
			join fetch recommendation.pet pet
			join fetch recommendation.tutor tutor
			join fetch recommendation.veterinarian veterinarian
			where lower(tutor.email) = lower(:email)
				or lower(veterinarian.email) = lower(:email)
			order by recommendation.createdAt desc
			""")
	List<CareRecommendation> findVisibleForUser(@Param("email") String email);

	@Query("""
			select recommendation from CareRecommendation recommendation
			join fetch recommendation.pet pet
			join fetch recommendation.tutor tutor
			join fetch recommendation.veterinarian veterinarian
			order by recommendation.createdAt desc
			""")
	List<CareRecommendation> findAllVisible();
}
