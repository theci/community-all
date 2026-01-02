import { TextareaHTMLAttributes, forwardRef, useState, useEffect } from 'react';

export interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  helperText?: string;
  fullWidth?: boolean;
  showCharCount?: boolean;
  autoResize?: boolean;
}

const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ label, error, helperText, fullWidth = true, showCharCount, autoResize, className = '', id, disabled, maxLength, value, onChange, ...props }, ref) => {
    const textareaId = id || `textarea-${Math.random().toString(36).substr(2, 9)}`;
    const [charCount, setCharCount] = useState(0);

    useEffect(() => {
      if (value !== undefined && value !== null) {
        setCharCount(String(value).length);
      }
    }, [value]);

    const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
      if (showCharCount) {
        setCharCount(e.target.value.length);
      }

      if (autoResize && e.target) {
        e.target.style.height = 'auto';
        e.target.style.height = `${e.target.scrollHeight}px`;
      }

      if (onChange) {
        onChange(e);
      }
    };

    const baseStyles = 'block px-3 py-2 border rounded-lg text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-offset-0 transition-colors disabled:bg-gray-100 disabled:cursor-not-allowed resize-vertical';

    const errorStyles = error
      ? 'border-red-500 focus:ring-red-500 focus:border-red-500'
      : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500';

    const widthStyles = fullWidth ? 'w-full' : '';
    const resizeStyles = autoResize ? 'resize-none overflow-hidden' : '';

    const combinedClassName = `${baseStyles} ${errorStyles} ${widthStyles} ${resizeStyles} ${className}`.trim();

    return (
      <div className={fullWidth ? 'w-full' : ''}>
        {label && (
          <div className="flex items-center justify-between mb-1">
            <label
              htmlFor={textareaId}
              className="block text-sm font-medium text-gray-700"
            >
              {label}
              {props.required && <span className="text-red-500 ml-1">*</span>}
            </label>

            {showCharCount && maxLength && (
              <span className={`text-xs ${charCount > maxLength ? 'text-red-600' : 'text-gray-500'}`}>
                {charCount} / {maxLength}
              </span>
            )}
          </div>
        )}

        <textarea
          ref={ref}
          id={textareaId}
          disabled={disabled}
          value={value}
          maxLength={maxLength}
          onChange={handleChange}
          className={combinedClassName}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={
            error ? `${textareaId}-error` : helperText ? `${textareaId}-helper` : undefined
          }
          {...props}
        />

        {error && (
          <p id={`${textareaId}-error`} className="mt-1 text-sm text-red-600">
            {error}
          </p>
        )}

        {!error && helperText && (
          <p id={`${textareaId}-helper`} className="mt-1 text-sm text-gray-500">
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Textarea.displayName = 'Textarea';

export default Textarea;
