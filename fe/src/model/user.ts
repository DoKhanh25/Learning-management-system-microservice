export interface User{
  userId?:String;
  username?:String;
  email?:String;
  roles?:String[];
  groups?: String[];
  firstName?:String;
  lastName?:String;
  enable?: boolean;
  attributes?: Attribute;
}

export interface AddUser{
  username?:String;
  password?:String;
  email?:String;
  roles?:String[];
  groups?: String[];
  firstName?:String;
  lastName?:String;
  enable?: boolean;
  attributes?: Attribute;
}

export interface Attribute{
  phone?:String[];
  city?:String[];
  country?:String[];
  institution?:String[];
  department?:String[];
  address?:String[];

}
export interface UserSession{
  userId?: String;
  username?: String;
  ipAddress?:String;
  start?:Date;
  lastAccess?:Date;
  clients?: object;
}
