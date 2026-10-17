export function getFieldErrors(error) {
  const errors = error?.response?.data?.errors
  return errors && typeof errors === 'object' ? errors : null
}

export function getApiErrorMessage(error, fallback) {
  if (error?.response) {
    const { status, data } = error.response

    if (status === 401) {
      return 'Your session has expired. Please sign in again.'
    }

    if (data?.message) {
      return data.message
    }

    if (status === 404) {
      return 'The requested item could not be found. It may have been deleted.'
    }

    if (status >= 500) {
      return 'The server encountered an unexpected error. Please try again later.'
    }
  } else if (error?.request) {
    return 'Could not reach the backend server. Please make sure the API is running.'
  }

  return fallback
}
