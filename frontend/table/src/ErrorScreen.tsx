import './ErrorScreen.css';

export interface ErrorScreenProps {
	message: string;
	onRetry?: () => void;
}

export function ErrorScreen({ message, onRetry }: ErrorScreenProps) {
	return (
		<div className='error-screen'>
			<div className='error-icon'>✕</div>
			<p className='error-text'>{message}</p>
			{onRetry && (
				<button className='error-retry-button' onClick={onRetry}>
					再試行
				</button>
			)}
		</div>
	);
}
