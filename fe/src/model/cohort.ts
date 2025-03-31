export interface Cohort{
  id?: number,
  name?: string,
  description?: string,
  createdTime?: any,
  updatedTime?: any,
  available?: number
  cohortMembers? : any
}
export interface CohortMember{
  id?: number,
  cohort?: number,
  keycloakId?: string,
  available?: number,
  addedTime?: any
}
