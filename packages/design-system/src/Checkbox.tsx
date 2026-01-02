import { InputHTMLAttributes, forwardRef } from 'react';

export interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: string;
  helperText?: string;
  error?: string;
}

const Checkbox = forwardRef<HTMLInputElement, CheckboxProps>(
  ({ label, helperText, error, className = '', id, disabled, ...props }, ref) => {
    const checkboxId = id || `checkbox-${Math.random().toString(36).substr(2, 9)}`;

    const baseStyles = 'h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-2 focus:ring-blue-500 focus:ring-offset-0 transition-colors disabled:cursor-not-allowed disabled:opacity-50';

    const errorStyles = error ? 'border-red-500' : '';

    const combinedClassName = `${baseStyles} ${errorStyles} ${className}`.trim();

    return (
      <div>
        <div className="flex items-start">
          <div className="flex items-center h-5">
            <input
              ref={ref}
              id={checkboxId}
              type="checkbox"
              disabled={disabled}
              className={combinedClassName}
              aria-invalid={error ? 'true' : 'false'}
              aria-describedby={
                error ? `${checkboxId}-error` : helperText ? `${checkboxId}-helper` : undefined
              }
              {...props}
            />
          </div>

          {label && (
            <div className="ml-3">
              <label
                htmlFor={checkboxId}
                className={`text-sm font-medium ${disabled ? 'text-gray-400' : 'text-gray-700'}`}
              >
                {label}
              </label>
              {helperText && !error && (
                <p id={`${checkboxId}-helper`} className="text-xs text-gray-500 mt-0.5">
                  {helperText}
                </p>
              )}
            </div>
          )}
        </div>

        {error && (
          <p id={`${checkboxId}-error`} className="mt-1 text-sm text-red-600">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Checkbox.displayName = 'Checkbox';

export default Checkbox;
