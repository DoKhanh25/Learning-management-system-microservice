import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Result } from '../../../model/result';
import { CodingQuestion, EssayQuestion } from '../../../model/quiz';

@Injectable({
  providedIn: 'root'
})
export class QuizService {
  baseUrl = "http://localhost:8090/quiz/api"

  constructor(public httpClient: HttpClient) { }

  // Question Bank endpoints
  getAllQuestionBanksByCourseId(courseId: any): Observable<Result> {
    return this.httpClient.get<Result>(`${this.baseUrl}/getAllQuestionBanksByCourseId?courseId=${courseId}`);
  }

  createQuestionBank(questionBankDTO: any): Observable<Result> {
    return this.httpClient.post<Result>(`${this.baseUrl}/createQuestionBank`, questionBankDTO);
  }

  deleteQuestionBank(id: any): Observable<Result> {
    return this.httpClient.delete<Result>(`${this.baseUrl}/deleteQuestionBank/${id}`);
  }

  // Question endpoints
  getQuestionsByQuestionBankId(questionBankId: any): Observable<Result> {
    return this.httpClient.get<Result>(`${this.baseUrl}?questionBankId=${questionBankId}`);
  }

  addMultipleChoiceQuestion(questionDTO: any): Observable<Result> {
    return this.httpClient.post<Result>(`${this.baseUrl}/addMultipleChoiceQuestion`, questionDTO);
  }

  addEssayQuestion(questionDTO: EssayQuestion): Observable<Result> {
    return this.httpClient.post<Result>(`${this.baseUrl}/addEssayQuestion`, questionDTO);
  }

  addCodingQuestion(questionDTO: CodingQuestion): Observable<Result> {
    return this.httpClient.post<Result>(`${this.baseUrl}/addCodingQuestion`, questionDTO);
  }

  importEssayQuestionsFromExcel(file: File, questionBankId: any): Observable<Result> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('questionBankId', questionBankId.toString());
    return this.httpClient.post<Result>(`${this.baseUrl}/importEssayQuestionsFromExcel`, formData);
  }

  updateMultipleChoiceQuestion(id: any, questionDTO: any): Observable<Result> {
    return this.httpClient.put<Result>(`${this.baseUrl}/updateMultipleChoiceQuestion/${id}`, questionDTO);
  }

  updateEssayQuestion(id: any, questionDTO: any): Observable<Result> {
    return this.httpClient.put<Result>(`${this.baseUrl}/updateEssayQuestion/${id}`, questionDTO);
  }

  updateCodingQuestion(id: any, questionDTO: any): Observable<Result> {
    return this.httpClient.put<Result>(`${this.baseUrl}/updateCodingQuestion/${id}`, questionDTO);
  }

  getQuestionBankByQuestionBankId(id: any): Observable<Result> {
    return this.httpClient.get<Result>(`${this.baseUrl}/getQuestionBankByQuestionBankId?id=${id}`);
  }

  deleteQuestion(id: any): Observable<Result> {
    return this.httpClient.delete<Result>(`${this.baseUrl}/deleteQuestion/${id}`);
  }

  downloadEssayQuestionsTemplate(): Observable<Blob> {
    return this.httpClient.get(`${this.baseUrl}/downloadEssaySample`, { responseType: 'blob' });
  }

  downloadMultipleChoiceQuestionsTemplate(): Observable<Blob> {
    return this.httpClient.get(`${this.baseUrl}/downloadMultipleChoiceSample`, { responseType: 'blob' });
  }

  importMultipleChoiceQuestionsFromExcel(file: File, questionBankId: any): Observable<Result> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('questionBankId', questionBankId.toString());
    return this.httpClient.post<Result>(`${this.baseUrl}/importMultipleChoiceQuestionsFromExcel`, formData);
  }
}
