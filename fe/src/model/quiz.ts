// Enums
export enum QuestionType {
  MULTIPLE_CHOICE = 'MULTIPLE_CHOICE',
  ESSAY = 'ESSAY',
  CODING = 'CODING'
}

export enum ExamType {
    MULTIPLE_CHOICE = 'MULTIPLE_CHOICE',
    ESSAY = 'ESSAY',
    CODING = 'CODING'
}

// Base interfaces
export interface Question {
  id?: number;
  text?: string;
  points?: number;
  difficultyLevel?: string;
  questionType?: QuestionType;
  userId?: string;
  questionBankId?: number;
}

export interface QuestionSubmission {
  id?: number;
  examSubmissionId?: number;
  questionId?: number;
  score?: number;
  submittedAt?: Date;
  graded?: boolean;
  feedback?: string;
}

// Extended question types
export interface MultipleChoiceQuestion extends Question {
  allowMultipleAnswers?: boolean;
  options?: MultipleChoiceOption[];
}

export interface EssayQuestion extends Question {
  wordLimit?: number;
  sampleAnswer?: string;
  rubric?: string;
}

export interface CodingQuestion extends Question {
  programmingLanguage?: string;
  starterCode?: string;
  solutionCode?: string;
  testCases?: string;
}

// Extended submission types
export interface MultipleChoiceSubmission extends QuestionSubmission {
  selectedOptionIds?: number[];
}

export interface EssaySubmission extends QuestionSubmission {
  answerText?: string;
  wordCount?: number;
}

export interface CodingSubmission extends QuestionSubmission {
  submittedCode?: string;
  testResults?: string;
  compilationOutput?: string;
  executionTime?: number;
}

// Question bank
export interface QuestionBank {
  id?: number;
  name?: string;
  description?: string;
  courseId?: number;
  userId?: string;
  questionType?: QuestionType;
  questions?: Question[];
}

export interface MultipleChoiceOption {
  id?: number;
  text?: string;
  isCorrect?: boolean;
  displayOrder?: number;
  questionId?: number;
}

// Exam and exam-related interfaces
export interface Exam {
  id?: number;
  courseId?: number;
  name?: string;
  description?: string;
  examType?: ExamType;
  duration?: number;
  totalScore?: number;
  shuffleQuestions?: boolean;
  shuffleAnswers?: boolean;
  createdTime?: Date;
  startTime?: Date;
  endTime?: Date;
  examQuestions?: ExamQuestion[];
}

export interface ExamQuestion {
  id?: number;
  examId?: number;
  questionId?: number;
  question?: Question;
  questionOrder?: number;
  points?: number;
}

export interface ExamSubmission {
  id?: number;
  userId?: string;
  examId?: number;
  startTime?: Date;
  submissionTime?: Date;
  totalScore?: number;
  isGraded?: boolean;
  gradedBy?: number;
  gradingTime?: Date;
  feedback?: string;
  questionSubmissions?: QuestionSubmission[];
}
