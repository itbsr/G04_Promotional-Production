import './EmptyOrderHistory.css';

export function EmptyOrderHistory() {
	return (
		<div className='empty-order-history'>
			<div className='empty-order-icon'>
				<svg viewBox='0 0 64 64' width='80' height='80'>
					<rect x='16' y='8' width='32' height='48' rx='2' fill='none' stroke='#ccc' strokeWidth='3' />
					<line x1='24' y1='18' x2='40' y2='18' stroke='#ccc' strokeWidth='2' strokeLinecap='round' />
					<line x1='24' y1='26' x2='40' y2='26' stroke='#ccc' strokeWidth='2' strokeLinecap='round' />
					<line x1='24' y1='34' x2='40' y2='34' stroke='#ccc' strokeWidth='2' strokeLinecap='round' />
					<line x1='24' y1='42' x2='32' y2='42' stroke='#ccc' strokeWidth='2' strokeLinecap='round' />
				</svg>
			</div>
			<p className='empty-order-text'>注文履歴がありません</p>
			<p className='empty-order-subtext'>注文するとここに表示されます</p>
		</div>
	);
}
