import {Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {Policy} from "../../../../model/role";
import {RoleService} from "../../../services/role/role.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-permission-list',
  templateUrl: './permission-list.component.html',
  styleUrl: './permission-list.component.css'
})
export class PermissionListComponent implements OnInit{
  items: MenuItem[] | undefined;
  loading = true;

  searchQuery: any;
  first = 0;
  rows = 10;
  policies: Policy[] = []
  selectedPolicies: any

  constructor(private roleService: RoleService,
              private router: Router,
              private messageService: MessageService
  ) {

  }

  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản lý quyền', url: '/admin/permission-management' }
      ];


    this.roleService.getClientPolicies().subscribe((result) => {
      let policies: Policy[] = result.data
      for (let i = 0; i < policies.length; i++) {
        if(policies[i].type == "scope"){
          this.roleService.getAssociatedPolicies(policies[i].id || "").subscribe((rs) => {
            policies[i].policies = rs.data;
            this.policies.push(policies[i]);
            this.loading = false;
          })
        }
      }
    });
  }

  checkAssociatedPolicies(policies: any[]): any{
    if(policies.length == 0){
      return ""
    } else {
      return policies[0].name
    }
  }


  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
  }


  clickPermissionDetail(id: string){
    this.router.navigate(['admin/permission-detail', id]);
  }
  clickAddPermission(){
    this.router.navigate(['admin/permission-add'])
  }

  deletePolicies(){
    let policyIds = []
    for (const id in this.selectedPolicies) {
      policyIds.push(this.selectedPolicies[id].id);
    }
    this.roleService.deleteClientPolicies(policyIds).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add(
          {severity: 'success',
            summary: 'thành công',
            detail: 'thành công',
            life: 2000
          });
        this.resetData()
      }
    }, (err) => {
      this.messageService.add(
        {severity: 'error',
          summary: 'Không thành công',
          detail: err.detail,
          life: 2000
        });
    })
  }

  resetData(){
    this.selectedPolicies = null;
    this.ngOnInit();
    this.policies = []
  }

}
