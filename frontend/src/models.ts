export interface User {
  id: number;
  name: string;
  accessLvl: number;
  roles: string[];
}

export interface FunctionDTO {
  id: number;
  name: string;
  format: number | null;
  funcResult: string;
  user: {
    id: number;
    name: string;
  };
}

export interface PointDTO {
  id: number;
  indexInFunction: number;
  x: number;
  y: number;
  functionId: number;
}