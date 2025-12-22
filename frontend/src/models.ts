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

export interface CreateFunctionRequest {
  name: string;
  format: number | null;
  userId: number;
  funcResult: string;
}

export interface CreateFunctionFromArrayRequest {
  name: string;
  userId: number;
 funcResult: string;
  points: { x: number; y: number }[];
}

export interface ChartData {
  x: number;
  y: number;
}