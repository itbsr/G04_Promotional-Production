export function Modal({ isOpen, children }: { isOpen: boolean; children: React.ReactNode }) {
	return (
		isOpen && (
			<div className='modal-wrapper'>
				<div onClick={(e) => e.stopPropagation()}>{children}</div>
			</div>
		)
	);
}
