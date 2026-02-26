import './LoadingScreen.css';

export function LoadingSpinner({ message }: { message: string }) {
	return (
		<div className='loading-screen'>
			<div className='loading-spinner'></div>
			<p className='loading-text'>{message}</p>
		</div>
	);
}
