import { ReactNode } from 'react';

export interface RadioGroupProps {
  label?: string;
  error?: string;
  helperText?: string;
  children: ReactNode;
  name: string;
  required?: boolean;
}

export default function RadioGroup({
  label,
  error,
  helperText,
  children,
  name,
  required,
}: RadioGroupProps) {
  const groupId = `radiogroup-${name}`;

  return (
    <div role="radiogroup" aria-labelledby={label ? `${groupId}-label` : undefined}>
      {label && (
        <div id={`${groupId}-label`} className="block text-sm font-medium text-gray-700 mb-2">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </div>
      )}

      <div className="space-y-2">
        {children}
      </div>

      {error && (
        <p className="mt-2 text-sm text-red-600">
          {error}
        </p>
      )}

      {!error && helperText && (
        <p className="mt-2 text-sm text-gray-500">
          {helperText}
        </p>
      )}
    </div>
  );
}
