import {Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {Policy, Role} from "../../../../model/role";
import {RoleService} from "../../../services/role/role.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-policy-list',
  templateUrl: './policy-list.component.html',
  styleUrl: './policy-list.component.css'
})
export class PolicyListComponent implements OnInit{

  items: MenuItem[] | undefined;
  searchQuery!:string;
  loading = true;
  first = 0;
  rows = 10;
  policies!: Policy[]
  selectedPolicies: any

  constructor(private roleService: RoleService,
              private router: Router,
              private messageService: MessageService) {
  }

  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản lý chính sách', url: '/admin/policy-management' }
      ];

    this.roleService.getClientPolicies().subscribe((result) => {
      let policies: Policy[] = result.data
      this.policies = [];

      for (let i = 0; i < policies.length; i++) {
        if(policies[i].type != "resource" && policies[i].type != "scope"){

          this.roleService.getDependentPermission(policies[i].id || "").subscribe((rs) => {
            policies[i].policies = rs.data;
            this.policies.push(policies[i]);
            this.loading = false;
          })
        }
      }
    })
  }

  checkDependentPermissions(policies: any[]): any{
    if(policies.length == 0){
      return ""
    } else {
      return policies[0].name
    }
  }

  clickPolicyDetail(id: string){
    this.router.navigate(['admin/policy-detail', id]);

  }

  clickAddPolicy(){
    this.router.navigate(['admin/policy-add'])
  }

  pageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
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
  }


}
