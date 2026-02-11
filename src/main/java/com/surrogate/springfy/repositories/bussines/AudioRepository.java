package com.surrogate.springfy.repositories.bussines;

import com.surrogate.springfy.models.DTO.AudioDTO;
import com.surrogate.springfy.models.bussines.Audio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AudioRepository extends JpaRepository<Audio, Long> {
    @Query("Select a from Audio a where a.audioId = :audioId and a.tipo= :mp3")
    Audio findByAudioMp3Id(String audioId, String mp3);

    @Query("Select (count(a )>0) from Audio a where a.audioId= :videoId")
    boolean existsAudioByAudioId(String videoId);
    @Query("Select new com.surrogate.springfy.models.DTO.AudioDTO(a.nombreaudio,a.path, a.audioId) from Audio a where lower(trim(a.tipo)) = 'mp3'\n")
    List<AudioDTO> findAllAudiosMp3();
    @Query("Select new com.surrogate.springfy.models.DTO.AudioDTO(a.nombreaudio,a.path, a.audioId) from Audio a where lower(trim(a.tipo)) = 'wav'\n")
    List<AudioDTO> findAllAudiosWav();


}
