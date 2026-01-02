import { LabelHTMLAttributes } from 'react';

export interface FormLabelProps extends LabelHTMLAttributes<HTMLLabelElement> {
  required?: boolean;
  optional?: boolean;
}

export default function FormLabel({
  required,
  optional,
  className = '',
  children,
  ...props
}: FormLabelProps) {
  return (
    <label
      className={`block text-sm font-medium text-gray-700 mb-1 ${className}`.trim()}
      {...props}
    >
      {children}
      {required && <span className="text-red-500 ml-1">*</span>}
      {optional && <span className="text-gray-500 ml-1 font-normal">(선택)</span>}
    </label>
  );
}
