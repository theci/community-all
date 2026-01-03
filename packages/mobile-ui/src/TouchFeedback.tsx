'use client';

import { useState, useCallback } from 'react';

interface TouchFeedbackProps {
  children: React.ReactNode;
  onTap?: () => void;
  className?: string;
}

/**
 * Touch feedback component
 * Provides visual feedback on touch interactions
 */
export default function TouchFeedback({ children, onTap, className = '' }: TouchFeedbackProps) {
  const [isPressed, setIsPressed] = useState(false);

  const handleTouchStart = useCallback(() => {
    setIsPressed(true);
  }, []);

  const handleTouchEnd = useCallback(() => {
    setIsPressed(false);
    if (onTap) {
      onTap();
    }
  }, [onTap]);

  return (
    <div
      className={`transition-opacity ${isPressed ? 'opacity-60' : 'opacity-100'} ${className}`}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      onTouchCancel={() => setIsPressed(false)}
    >
      {children}
    </div>
  );
}
