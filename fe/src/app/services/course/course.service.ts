import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs";
import {Result} from "../../../model/result";
import {
  Course,
  CourseSection,
  Enrolment,
  Lesson,
  LessonBranch,
  LessonNote,
  LessonPages,
  LessonTimer
} from "../../../model/course";
import {Assignment, AssignmentSubmission} from "../../../model/assignment";

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  baseUrl = "http://localhost:8090/course/api"

  constructor(public httpClient: HttpClient) { }

  getAllCourses():Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getAllCourses`)
  }

  addCourse(course: any): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/addCourse`, course);
  }

  getCourseById(id: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getCourseById?id=${id}`);
  }

  getAllUserEnrolmentsByCourseId(id: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getAllUserEnrolmentsByCourseId?id=${id}`);
  }

  getAllStudentEnrolmentsByCourseId(id: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getAllStudentEnrolmentsByCourseId?id=${id}`);
  }

  getEnrolByCourseAndEnrolTypeManual(id: any, enrolType: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getEnrolByCourseAndEnrolType?id=${id}&enrolType=${enrolType}`);
  }

  addEnrolmentsByCohort(cohortId: any, courseId: any, courseRole: any): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/addEnrolmentsByCohort?cohortId=${cohortId}&courseId=${courseId}&courseRole=${courseRole}`, null);
  }

  updateCourse(id: any, courseData: any): Observable<Result> {
    return this.httpClient.put<Result>(`${this.baseUrl}/updateCourse?id=${id}`, courseData);
  }

  createSelfEnrol(enrolment: Enrolment): Observable<Result> {
    return this.httpClient.post<Result>(`${this.baseUrl}/createSelfEnrol`, enrolment);
  }

  deleteCourseById(id: any): Observable<Result>{
    return this.httpClient.delete<Result>(`${this.baseUrl}/deleteCourseById?id=${id}`);
  }

// teacher function

  getAllTeacherCoursesByUserId(userId: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/teacher/getAllCoursesByUserId`);
  }

  getAllStudentCoursesByUserId(): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/student/getAllCoursesByUserId`)
  }


  // demo

  addCourseSection(courseSection: CourseSection): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/addCourseSection`, courseSection);
  }

  addLesson(lesson: Lesson): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/addLesson`, lesson);
  }


  // assignment

  getAllAssignmentsByCourseId(courseId: string) {
    return this.httpClient.get<any>(`${this.baseUrl}/getAllAssignmentsByCourseId?courseId=${courseId}`);
  }

  addAssignment(assignmentData: Assignment) {
    return this.httpClient.post<any>(`${this.baseUrl}/addAssignment`, assignmentData);
  }

  updateAssignment(assignmentData: Assignment){
    return this.httpClient.put<Result>(`${this.baseUrl}/updateAssignment`, assignmentData)
  }

  deleteAssignmentById(assignmentId: number) {
    return this.httpClient.delete<any>(`${this.baseUrl}/deleteAssignmentById?assignmentId=${assignmentId}`);
  }

  // assignment
  findLessonEntitiesBySectionId(sectionId: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/findLessonEntitiesBySectionId?sectionId=${sectionId}`);
  }

  findLessonEntitiesByCourseId(courseId: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/findLessonEntitiesByCourseId?courseId=${courseId}`);
  }

  getTeacherCourseById(id: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/teacher/getCourseById?id=${id}`);
  }

  getLessonById(id: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getLessonById?id=${id}`);
  }

  getLessonPagesByLessonId(id: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getLessonPagesByLessonId?lessonId=${id}`);
  }

  updateCourseSection(courseSection: CourseSection){
    return this.httpClient.put<Result>(`${this.baseUrl}/updateCourseSection`, courseSection);
  }

  deleteCourseSectionById(id: any): Observable<Result>{
    return this.httpClient.delete<Result>(`${this.baseUrl}/deleteCourseSectionById?id=${id}`);
  }

  getDocumentFileByLessonPagesId(id: any): Observable<any>{
    return this.httpClient.get<any>(`${this.baseUrl}/getDocumentFileByLessonPagesId?id=${id}`);
  }

  addContentLessonPage(lessonPage: LessonPages): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/addContentLessonPage`, lessonPage);
  }

  addDocumentLessonPage(lessonPageData: LessonPages, file: File): Observable<Result> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', lessonPageData.title || "title");
    formData.append('qType', lessonPageData.qType || "DOCUMENT");
    formData.append('content', lessonPageData.content || '');
    formData.append('position', lessonPageData?.position?.toString() || "1" );
    formData.append('lessonId', lessonPageData?.lessonId?.toString() || "");
    return this.httpClient.post<Result>(`${this.baseUrl}/addDocumentLessonPage`, formData);
  }

  addUploadVideoLessonPage(lessonPageData: LessonPages, file: File | null): Observable<Result> {
    const formData = new FormData();
    formData.append('file', file || new File([""], "No file"));
    formData.append('title', lessonPageData.title || "title");
    formData.append('qType', lessonPageData.qType || "VIDEO");
    formData.append('content', lessonPageData.content || '');
    formData.append('position', lessonPageData?.position?.toString() || "1" );
    formData.append('lessonId', lessonPageData?.lessonId?.toString() || "");
    return this.httpClient.post<Result>(`${this.baseUrl}/addUploadVideoLessonPage`, formData);
  }

  addVideoURLLessonPage(lessonPageData: LessonPages): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/addVideoURLLessonPage`, lessonPageData);
  }

  deleteLessonPageById(id: any): Observable<Result>{
    return this.httpClient.delete<Result>(`${this.baseUrl}/deleteLessonPageById?id=${id}`);
  }

  getVideoFileLessonPagesById(id: any, range: string): Observable<HttpResponse<Blob>> {
    let headers = new HttpHeaders();
    if (range) {
      headers = headers.set('range', range);
    }
    return this.httpClient.get(`${this.baseUrl}/getVideoFileLessonPagesById?id=${id}`, {
      headers: headers,
      responseType: "blob",
      observe: "response"
    })
  }

  saveLessonNote(lessonNote: LessonNote): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/saveLessonNote`, lessonNote);
  }

  getLessonNoteByLessonPageIdAndUserId(lessonPageId: any): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getLessonNoteByLessonPageIdAndUserId?lessonPagesId=${lessonPageId}`);
  }

  deleteLessonNote(lessonNoteId: any): Observable<Result>{
    return this.httpClient.delete<Result>(`${this.baseUrl}/deleteLessonNote?lessonNoteId=${lessonNoteId}`);
  }

  saveLessonTimer(lessonTimer: LessonTimer): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/saveLessonTimer`, lessonTimer);
  }

  saveLessonBranch(lessonBranch: LessonBranch): Observable<Result>{
    return this.httpClient.post(`${this.baseUrl}/saveLessonBranch`, lessonBranch);
  }

  getStudentProgressByLessonIdAndUserId(lessonId: any, userId: any){
    return this.httpClient.get<Result>(`${this.baseUrl}/getStudentProgressByLessonIdAndUserId?lessonId=${lessonId}&userId=${userId}`);
  }

  getAssignmentSubmissionByUserIdAndAssignmentId(assignmentId: any, userId: any){
    return this.httpClient.get<Result>(`${this.baseUrl}/getAssignmentSubmissionByUserIdAndAssignmentId?assignmentId=${assignmentId}&userId=${userId}`);
  }

  getAssignmentSubmissions(assignmentId: any){
    return this.httpClient.get<Result>(`${this.baseUrl}/getAssignmentSubmissions?assignmentId=${assignmentId}`);
  }

  addAssignmentSubmissionText(submission: AssignmentSubmission){
    return this.httpClient.post<any>(`${this.baseUrl}/addAssignmentSubmissionText`, submission)
  }

  addAssignmentSubmissionFiles(formData: FormData){
    return this.httpClient.post<any>(`${this.baseUrl}/addAssignmentSubmissionFiles`, formData);
  }

  getAssignmentById(assignmentId: any){
    return this.httpClient.get<Result>(`${this.baseUrl}/getAssignmentById?assignmentId=${assignmentId}`);
  }

  updateSubmissionGrade(gradeData: any): Observable<Result> {
    return this.httpClient.put<Result>(`${this.baseUrl}/updateSubmissionGrade`, gradeData);
  }

  downloadSubmissionFile(submissionId: number, fileIndex: number): Observable<Blob> {
    return this.httpClient.get(`${this.baseUrl}/downloadSubmissionFile?submissionId=${submissionId}&fileIndex=${fileIndex}`, {
      responseType: 'blob'
    });
  }


}
