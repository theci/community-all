import { HTMLAttributes } from 'react';

export interface FormErrorProps extends HTMLAttributes<HTMLParagraphElement> {
  children?: React.ReactNode;
  show?: boolean;
}

export default function FormError({
  children,
  show = true,
  className = '',
  ...props
}: FormErrorProps) {
  if (!show || !children) return null;

  return (
    <p
      className={`mt-1 text-sm text-red-600 ${className}`.trim()}
      role="alert"
      {...props}
    >
      {children}
    </p>
  );
}
