import {Course} from "./course";

export interface Assignment{
  id?: number,
  name?: string,
  course?: Course,
  description?: string,
  courseId?: number,
  assignmentSubmissions?: AssignmentSubmission[]
  assignmentType?: string,
  resubmit?: boolean,
  preventLate?: boolean,
  startDate?: any,
  endDate?: any
}

export interface AssignmentSubmission{
  id?: number,
  userId?: string,
  assignment?: Assignment,
  assignmentId?: number,
  numfiles?: number,
  data1?: string,
  data2?: string,
  grade?: number,
  submissionComment?: string,
  updatedTime?: any,
  createdTime?: any
}
