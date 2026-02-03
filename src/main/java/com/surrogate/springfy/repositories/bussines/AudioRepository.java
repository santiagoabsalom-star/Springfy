package com.surrogate.springfy.repositories.bussines;

import com.surrogate.springfy.models.DTO.AudioDTO;
import com.surrogate.springfy.models.bussines.Audio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AudioRepository extends JpaRepository<Audio, Long> {

    Audio findByAudioId(String audioId);
    @Query("Select (count(a )>0) from Audio a where a.audioId= :videoId")
    boolean existsAudioByAudioId(String videoId);
    @Query("Select new com.surrogate.springfy.models.DTO.AudioDTO(a.nombreaudio,a.path,a.audioId) from Audio a")
    List<AudioDTO> findAllAudios();
}
