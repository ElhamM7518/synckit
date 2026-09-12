export class ApiError extends Error {
  readonly status: number;
  readonly fields?: Record<string, string>;

  constructor(status: number, message: string, fields?: Record<string, string>) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fields = fields;
  }
}

type ProblemDetail = {
  detail?: string;
  title?: string;
  status?: number;
  fields?: Record<string, string>;
};

type RequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  token?: string | null;
};

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);

  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  if (options.token) {
    headers.set("Authorization", `Bearer ${options.token}`);
  }

  const response = await fetch(path, {
    ...options,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (!response.ok) {
    let message = response.statusText || "Request failed";
    let fields: Record<string, string> | undefined;

    try {
      const problem = (await response.json()) as ProblemDetail;
      message = problem.detail ?? problem.title ?? message;
      fields = problem.fields;
    } catch {
      fields = undefined;
    }

    throw new ApiError(response.status, message, fields);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
