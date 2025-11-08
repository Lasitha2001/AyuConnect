import React from 'react';
interface AvatarProps {
  src: string;
  alt: string;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}
const Avatar: React.FC<AvatarProps> = ({
  src,
  alt,
  size = 'md',
  className = ''
}) => {
  const sizeClasses = {
    sm: 'h-8 w-8',
    md: 'h-10 w-10',
    lg: 'h-12 w-12'
  };
  return <img src={src} alt={alt} className={`rounded-full object-cover ${sizeClasses[size]} ${className}`} />;
};
export default Avatar;