import { forwardRef, type InputHTMLAttributes } from 'react';

interface Props extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  helper?: string;
}

/**
 * Form field composed of label + input + helper text + inline error message.
 * Uses forwardRef so React Hook Form's `register` can attach directly.
 */
export const Input = forwardRef<HTMLInputElement, Props>(function Input(
  { label, error, helper, id, className = '', ...rest },
  ref,
) {
  const inputId = id ?? rest.name ?? label.toLowerCase().replace(/\s+/g, '-');
  const describedBy = [helper ? `${inputId}-helper` : null, error ? `${inputId}-error` : null]
    .filter(Boolean)
    .join(' ');
  return (
    <div className="mb-4">
      <label htmlFor={inputId} className="mb-1 block text-sm font-medium text-slate-700">
        {label}
      </label>
      <input
        ref={ref}
        id={inputId}
        aria-invalid={Boolean(error)}
        aria-describedby={describedBy || undefined}
        className={`w-full rounded-lg border bg-white px-3 py-2 text-sm shadow-sm transition
          focus:outline-none focus:ring-2 focus:ring-red-300
          ${error ? 'border-red-500' : 'border-slate-300'} ${className}`}
        {...rest}
      />
      {helper && !error && (
        <p id={`${inputId}-helper`} className="mt-1 text-xs text-slate-500">
          {helper}
        </p>
      )}
      {error && (
        <p id={`${inputId}-error`} role="alert" className="mt-1 text-xs font-medium text-red-600">
          {error}
        </p>
      )}
    </div>
  );
});
