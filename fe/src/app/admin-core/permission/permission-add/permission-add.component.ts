import {Component, OnInit} from '@angular/core';
import {MenuItem, MessageService} from "primeng/api";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {RoleService} from "../../../services/role/role.service";
import {Policy, Resource, ScopePermission} from "../../../../model/role";

@Component({
  selector: 'app-permission-add',
  templateUrl: './permission-add.component.html',
  styleUrl: './permission-add.component.css'
})
export class PermissionAddComponent implements OnInit{
  items: MenuItem[] | undefined;
  constructor(private fb: FormBuilder,
              private roleService: RoleService,
              private messageService: MessageService) {
  }

  permissionForm!: FormGroup;
  scopes!: string[]
  policies!: Policy[]
  resources!: Resource[]

  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản lý quyền', url: '/admin/permission-management' },
        {label: 'Tạo quyền'}
      ];

    this.scopes = []
    this.policies = []

    this.permissionForm = this.fb.group({
      name: ["", [Validators.required]],
      description: [""],
      resource: [""],
      scopes: new FormControl<string[] | null>([]),
      policies: new FormControl<string[] | null>([]),
      decisionStrategy: [""]
    })


    this.roleService.getClientResources().subscribe((result) => {
      let resourceNames: Resource[] = [];
      if(result.data != null){
        for (let i = 0; i < result.data.length; i++){
          let object: Resource = {
            id: result.data[i]._id,
            name: result.data[i].name
          }
          resourceNames.push(object)
        }
      }
      this.resources = resourceNames;
    })

    this.roleService.getClientPolicies().subscribe((result) => {
      let policies: Policy[] = result.data
      console.log(policies)
      for (let i = 0; i < policies.length; i++) {
        if(policies[i].type != "resource" && policies[i].type != "scope") {
          this.policies.push(policies[i]);
        }
      }
    })
  }

  onResourceChange(event: Event){
    const selectedResourceId =  (event.target as HTMLSelectElement).value;
    this.roleService.getScopesByResource(selectedResourceId).subscribe((result) => {
      let scopeNames: string[] = [];
      if(result.data != null){
        for (let i = 0; i < result.data.length; i++) {
          scopeNames.push(result.data[i].name);
        }
      }
      this.scopes = scopeNames;
    })

  }

  clickAddPermission(){
    // console.log(this.permissionForm.get("name")?.value)
    // console.log(this.permissionForm.get("description")?.value)
    // console.log(this.permissionForm.get("resource")?.value)
    // console.log(this.permissionForm.get("scopes")?.value)
    // console.log(this.permissionForm.get("policies")?.value)
    // console.log(this.permissionForm.get("decisionStrategy")?.value)

    let scopePermission: ScopePermission = {
      name: this.permissionForm.get("name")?.value,
      description: this.permissionForm.get("description")?.value,
      resource: this.permissionForm.get("resource")?.value,
      scopes: this.permissionForm.get("scopes")?.value,
      policies: this.permissionForm.get("policies")?.value,
      decisionStrategy: this.permissionForm.get("decisionStrategy")?.value
    }

    this.roleService.addScopePermission(scopePermission).subscribe((result) => {
      if(result.status == 1){
        this.messageService.add({
          severity: 'success',
          summary: 'Thành công',
          detail: 'Tạo thành công',
          life: 2000
        })
      } else {
        this.messageService.add(
          {severity: 'warn',
            summary: 'Không thành công',
            life: 2000
          });
      }
    })
  }
}
