import { InputHTMLAttributes, forwardRef } from 'react';

export interface RadioProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: string;
  helperText?: string;
  error?: string;
}

const Radio = forwardRef<HTMLInputElement, RadioProps>(
  ({ label, helperText, error, className = '', id, disabled, ...props }, ref) => {
    const radioId = id || `radio-${Math.random().toString(36).substr(2, 9)}`;

    const baseStyles = 'h-4 w-4 border-gray-300 text-blue-600 focus:ring-2 focus:ring-blue-500 focus:ring-offset-0 transition-colors disabled:cursor-not-allowed disabled:opacity-50';

    const errorStyles = error ? 'border-red-500' : '';

    const combinedClassName = `${baseStyles} ${errorStyles} ${className}`.trim();

    return (
      <div>
        <div className="flex items-start">
          <div className="flex items-center h-5">
            <input
              ref={ref}
              id={radioId}
              type="radio"
              disabled={disabled}
              className={combinedClassName}
              aria-invalid={error ? 'true' : 'false'}
              aria-describedby={
                error ? `${radioId}-error` : helperText ? `${radioId}-helper` : undefined
              }
              {...props}
            />
          </div>

          {label && (
            <div className="ml-3">
              <label
                htmlFor={radioId}
                className={`text-sm font-medium ${disabled ? 'text-gray-400' : 'text-gray-700'}`}
              >
                {label}
              </label>
              {helperText && !error && (
                <p id={`${radioId}-helper`} className="text-xs text-gray-500 mt-0.5">
                  {helperText}
                </p>
              )}
            </div>
          )}
        </div>

        {error && (
          <p id={`${radioId}-error`} className="mt-1 text-sm text-red-600">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Radio.displayName = 'Radio';

export default Radio;
