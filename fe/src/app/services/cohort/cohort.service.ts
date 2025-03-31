import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Result} from "../../../model/result";
import {Cohort} from "../../../model/cohort";

@Injectable({
  providedIn: 'root'
})
export class CohortService {
  baseUrl = "http://localhost:8090/user/api"


  constructor(public httpClient: HttpClient) { }

  getAllCohorts(): Observable<Result>{
    return this.httpClient.get(`${this.baseUrl}/getAllCohorts`)
  }

  addCohort(cohort: any): Observable<Result>{
    return this.httpClient.post(`${this.baseUrl}/addCohort`, cohort);
  }

  getCohortById(id: string): Observable<Result>{
    return this.httpClient.get(`${this.baseUrl}/getCohortById?id=${id}`);
  }

  updateCohort(cohort: any): Observable<Result>{
    return this.httpClient.post(`${this.baseUrl}/updateCohort`, cohort);
  }
  deleteCohorts(ids: any): Observable<Result>{
    return this.httpClient.post(`${this.baseUrl}/deleteCohorts`, ids);
  }



}
