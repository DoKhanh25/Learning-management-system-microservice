import {Component, OnInit} from '@angular/core';
import {MenuItem} from "primeng/api";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Policy, Resource} from "../../../../model/role";
import {ActivatedRoute} from "@angular/router";
import {RoleService} from "../../../services/role/role.service";

@Component({
  selector: 'app-permission-detail',
  templateUrl: './permission-detail.component.html',
  styleUrl: './permission-detail.component.css'
})
export class PermissionDetailComponent implements OnInit{
  items: MenuItem[] | undefined;
  permissionForm!: FormGroup;
  scopes!: string[]
  policies!: Policy[]
  resources!: Resource[]
  permissionId!: string

  constructor(private route: ActivatedRoute,
              private roleService: RoleService,
              private fb: FormBuilder) {
    this.permissionId = this.route.snapshot.paramMap.get('id') || "";

  }
  ngOnInit() {
    this.items =
      [
        { icon: 'pi pi-home', route: '/' },
        { label: 'Quản lý quyền', url: '/admin/permission-management' },
        {label: 'Cập nhật quyền'}
      ];

    this.scopes = ["VIEW", "EDIT", "CREATE", "DELETE"]
    this.policies = []

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

    this.permissionForm = this.fb.group({
      name: ["", [Validators.required]],
      description: [""],
      resource: [""],
      scopes: new FormControl<string[] | null>([]),
      policies: new FormControl<string[] | null>([]),
      decisionStrategy: [""]
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



    this.roleService.getScopePermissionById(this.permissionId).subscribe((result) => {
      if(result.data != null){
        this.permissionForm.setValue({
          name: result.data.name,
          description: result.data.description,
          resource: result.data.resources[0],
          scopes: result.data.scopes,
          policies: result.data.policies,
          decisionStrategy: result.data.decisionStrategy
        })
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
}
