package org.example.quizservice.services;

import com.example.commondto.dto.ResultDTO;
import org.example.quizservice.dto.QuestionBankDTO;
import org.example.quizservice.entity.QuestionBankEntity;
import org.example.quizservice.mapper.QuestionBankMapper;
import org.example.quizservice.repository.QuestionBankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionBankService {

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private QuestionBankMapper questionBankMapper;


    public ResponseEntity<ResultDTO> getAllQuestionBanksByCourseId(Long courseId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();

        if(!validateUserPermission(courseId, validateUserId)){
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        List<QuestionBankEntity> questionBankEntityList = questionBankRepository.findByCourseId(courseId);
        List<QuestionBankDTO> questionBankDTOList = questionBankMapper.toDto(questionBankEntityList);

        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }


    public ResponseEntity<ResultDTO> saveQuestionBank(QuestionBankDTO questionBankDTO, String validateUserId) {

        ResultDTO resultDTO = new ResultDTO();

        if(!validateUserPermission(questionBankDTO.getCourseId(), validateUserId)){
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        QuestionBankEntity questionBankEntity = questionBankMapper.toEntity(questionBankDTO);
        questionBankEntity = questionBankRepository.save(questionBankEntity);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Question Bank saved successfully");
        resultDTO.setData(questionBankMapper.toDto(questionBankEntity));

        return ResponseEntity.status(HttpStatus.CREATED).body(resultDTO);
    }


    public ResponseEntity<ResultDTO> deleteQuestionBank(Long questionBankId, String validateUserId) {

        ResultDTO resultDTO = new ResultDTO();

        QuestionBankEntity questionBankEntity = questionBankRepository.findById(questionBankId).orElse(null);
        if(questionBankEntity == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if(!validateUserPermission(questionBankEntity.getCourseId(), validateUserId)){
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        questionBankRepository.delete(questionBankEntity);

        resultDTO.setStatus(1);
        resultDTO.setMessage("Question Bank deleted successfully");

        return ResponseEntity.status(HttpStatus.OK).body(resultDTO);
    }




    public Boolean validateUserPermission(Long courseId, String userId) {

        return true;
    }

}
