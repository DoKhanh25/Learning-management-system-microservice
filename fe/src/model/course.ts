export interface Course{
  id?: number,
  courseSections?: CourseSection[],
  enrols?: any[],
  showGrades: number,
  resources?: any[],
  name?: string,
  summary?: string,
  startDate?: any,
  endDate?: any
}

export interface Enrolment {
  courseId?: number,
  name?: string,
  status?: number,
  enrolType?: string,
  courseRole?: string,
  password?: string,
  enrolStartDate?: any,
  enrolEndDate?: any,
}

export interface UserEnrolments{
  id: number,
  status: number,
  enrol: Enrolment,
  userId: string,
  timeStart: any,
  timeEnd: any
}

export interface CourseSection{
  id?: number,
  name?: string,
  course?: Course,
  courseId?: number,
  lessons?: Lesson[],
  summary?: string,
  section?: number,
  createdTime?: any,
  updatedTime?: any,
}

export interface Lesson{
  id?: number,
  name?: string,
  intro?: string,
  section?: CourseSection,
  sectionId?: number,
  createdTime?: any,
  updatedTime?: any
}

export interface LessonPages{
  id?: number,
  lessonId?: number,
  lesson?: Lesson,
  lessonBranch?: LessonBranch[],
  lessonAttempts?: LessonAttempts[],
  position?: number,
  title?: string,
  qType?: string,
  content?: string,
  createdTime?: any,
  updatedTime?: any
}

export interface LessonAttempts{
  id?: number,
  lessonId?: number,
  lessonPages?: LessonPages,
  lessonPagesId?: number,
  userId?: string,
  timeSeen?: any,
  correct?: string,
  userAnswer?: string
}

export interface LessonBranch{
  id?: number,
  lessonPages?: LessonPages,
  lessonPagesId?: number,
  lessonId?: number,
  userId?: string,
  timeSeen?: number,
}

export interface LessonNote{
  id?: number,
  userId?: string,
  note?: string,
  createdTime?: any,
  updatedTime?: any,
  lessonPages?: LessonPages,
  lessonPagesId?: number,
}

export interface LessonTimer{
  id?: number,
  userId?: string,
  startTime?: any,
  lessonTime?: any,
  completed?: number,
  lesson?: Lesson,
  lessonId?: number
}

export interface StudentProgress{
  lessonBranchId?: number,
  lessonId?: number,
  lessonPagesId?: number,
  title?: string,
  userId?: string,
  completed?: number,
  startTime?: any
}
