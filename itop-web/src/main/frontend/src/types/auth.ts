export interface LoginRequest {
  username: string
  password: string
}

export interface JwtResponse {
  token: string
  type: string
  username: string
  email: string
  userId: number
}

export interface User {
  id: number
  username: string
  email: string
  firstName: string
  lastName: string
  roles: string[]
}