import { HTMLAttributes } from 'react';

export interface FormHelperTextProps extends HTMLAttributes<HTMLParagraphElement> {
  children?: React.ReactNode;
}

export default function FormHelperText({
  children,
  className = '',
  ...props
}: FormHelperTextProps) {
  if (!children) return null;

  return (
    <p
      className={`mt-1 text-sm text-gray-500 ${className}`.trim()}
      {...props}
    >
      {children}
    </p>
  );
}
