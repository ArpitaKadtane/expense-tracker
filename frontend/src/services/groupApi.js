import http from './http'

export async function getGroups() {
  const { data } = await http.get('/groups')
  return data
}

export async function createGroup({ name, memberUsernames = [] }) {
  const { data } = await http.post('/groups', { name, memberUsernames })
  return data
}

export async function getGroup(id) {
  const { data } = await http.get(`/groups/${id}`)
  return data
}

export async function addGroupMember(id, username) {
  const { data } = await http.post(`/groups/${id}/members`, { username })
  return data
}

export async function getGroupExpenses(id) {
  const { data } = await http.get(`/groups/${id}/expenses`)
  return data
}

export async function addGroupExpense(id, expense) {
  const { data } = await http.post(`/groups/${id}/expenses`, expense)
  return data
}

export async function getGroupBalances(id) {
  const { data } = await http.get(`/groups/${id}/balances`)
  return data
}

export async function getGroupSettleUp(id) {
  const { data } = await http.get(`/groups/${id}/settle-up`)
  return data
}
