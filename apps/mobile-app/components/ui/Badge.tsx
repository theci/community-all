import { HTMLAttributes, ReactNode } from 'react';

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info';
  size?: 'sm' | 'md' | 'lg';
  rounded?: boolean;
  children: ReactNode;
}

export default function Badge({
  variant = 'default',
  size = 'md',
  rounded = false,
  className = '',
  children,
  ...props
}: BadgeProps) {
  const variants = {
    default: 'bg-gray-100 text-gray-800',
    primary: 'bg-blue-100 text-blue-800',
    success: 'bg-green-100 text-green-800',
    warning: 'bg-yellow-100 text-yellow-800',
    danger: 'bg-red-100 text-red-800',
    info: 'bg-cyan-100 text-cyan-800',
  };

  const sizes = {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-2.5 py-1 text-sm',
    lg: 'px-3 py-1.5 text-base',
  };

  const roundedClass = rounded ? 'rounded-full' : 'rounded';

  return (
    <span
      className={`inline-flex items-center font-medium ${variants[variant]} ${sizes[size]} ${roundedClass} ${className}`.trim()}
      {...props}
    >
      {children}
    </span>
  );
}

export const BadgeDot = ({ className = '', ...props }: HTMLAttributes<HTMLSpanElement>) => (
  <span
    className={`w-2 h-2 rounded-full mr-1.5 ${className}`}
    {...props}
  />
);
