import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Result} from "../../../model/result";
import {Policy, RolePost, ScopePermission} from "../../../model/role";

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  baseUrl = "http://localhost:8090/user/api"

  constructor(public httpClient: HttpClient) { }

  getAllRoles():Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getAllRoles`)
  }

  addRole(role: RolePost):Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/addRole`, role);
  }

  deleteRoles(roles: string[]):Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/deleteRoles`, roles);
  }

  getCompositeRoles(id: string | undefined): Observable<Result> {
    return this.httpClient.get<Result>(`${this.baseUrl}/getCompositeRoles?parentRoleName=${id}`)
  }

  getRoleByName(roleName: string): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getRoleByName?roleName=${roleName}`)
  }
  getUsersInRole(roleName: string): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getUsersInRole?roleName=${roleName}`)
  }
  addComposites(roleName: string, roleNames: any): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/addComposites/${roleName}`, roleNames)
  }

  unsignedComposites(roleName: string, roleNames: any): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/unsignedComposites/${roleName}`, roleNames)
  }

  updateRoleDetail(role :any): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/updateRoleDetail`, role)
  }
  updateUsersInRole(roleName: string, userIds: string[]): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/updateUsersInRole/${roleName}`, userIds)
  }
  removeUsersInRole(roleName: string, userIds: string[]): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/removeUsersInRole/${roleName}`, userIds)
  }

  getClientResources():Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getClientResources`)
  }

  getClientPolicies():Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getClientPolicies`)
  }

  getDependentPermission(id: string):Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getDependentPermission?id=${id}`)
  }

  getClientPolicyById(id: string): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getClientPolicyById?id=${id}`)
  }

  updateClientPolicy(policy: Policy): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/updateClientPolicy`, policy);
  }

  addClientPolicy(policy: Policy): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/addClientPolicy`, policy);
  }

  deleteClientPolicies(ids: string[]): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/deleteClientPolicies`, ids);
  }

  getAssociatedPolicies(id: string): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getAssociatedPolicies?id=${id}`);
  }

  getScopesByResource(id: string): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getScopesByResource?id=${id}`);
  }

  addScopePermission(scopePermission: ScopePermission): Observable<Result>{
    return this.httpClient.post<Result>(`${this.baseUrl}/createPermission`, scopePermission);
  }

  getScopePermissionById(id: string | null): Observable<Result>{
    return this.httpClient.get<Result>(`${this.baseUrl}/getScopePermissionById/${id}`);

  }



}
