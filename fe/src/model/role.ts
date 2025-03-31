export interface Role{
  name?:string,
  description?:string,
  isComposite?:boolean,
  composites?:any,
  id?:string
}

export interface RolePolicy{
  name?:string,
  description?:string,
  isComposite?:boolean,
  composites?:any,
  id?:string,
  required?:boolean
}

export interface RolePost{
  name?:string,
  description?:string,
}

export interface Policy{
  id?:string,
  name?:string,
  type?:string,
  policies?:string,
  description?:string,
  decisionStrategy?:string,
  logic?:string,
  config?:any
}

export interface Resource{
  id?: string,
  name?: string
}

export interface ScopePermission{
  name?: string,
  description?: string,
  resource?: string,
  scopes?: string[],
  policies?: string[],
  decisionStrategy?: string
}
