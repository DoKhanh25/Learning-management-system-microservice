import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Result} from "../../../model/result";
import {AddUser, User} from "../../../model/user";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  baseUrl = "http://localhost:8090/user/api"

  constructor(public httpClient: HttpClient) { }
  getAllUsers():Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getAllUsers`)
  }

  getUserById(id: any): Observable<User>{
    return this.httpClient.get(`${this.baseUrl}/getUser?id=${id}`)
  }

  getUsersByIds(userIds: any): Observable<Result>{
    return this.httpClient.post(`${this.baseUrl}/getUsersByIds`, userIds)
  }

  getUserSessionById(id: any): Observable<any>{
    return this.httpClient.get(`${this.baseUrl}/getUserSession?id=${id}`)
  }

  addUserByExcel(formData: FormData): Observable<any>{
    return this.httpClient.post(`${this.baseUrl}/uploadUsersExcel`, formData, {
      responseType: 'blob'
    });
  }
  addUser(addUser: AddUser): Observable<Result>{
    return this.httpClient.post(`${this.baseUrl}/createUser`, addUser);
  }

  updateUser(updateUser: User, userId: String): Observable<Result> {
    return this.httpClient.post(`${this.baseUrl}/updateUser/${userId}`, updateUser)
  }

  disableUsers(userList: string[]): Observable<Result>{
    return this.httpClient.post(`${this.baseUrl}/disableUsers`, userList);
  }

  searchUsers(searchQuery: string): Observable<Result>{
    return this.httpClient.get(`${this.baseUrl}/searchUsers?searchQuery=${searchQuery}`);
  }



}
