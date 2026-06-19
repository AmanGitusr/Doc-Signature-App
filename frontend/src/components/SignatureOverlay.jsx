import React, { useEffect, useRef, useState } from 'react';
import { DndContext, useDraggable } from '@dnd-kit/core';

function SignatureStamp({ position }) {
  const { attributes, listeners, setNodeRef, transform } = useDraggable({ id: 'signature-stamp' });
  const style = {
    position: 'absolute',
    left: `${position.x}%`,
    top: `${position.y}%`,
    transform: transform ? `translate(${transform.x}px, ${transform.y}px)` : undefined
  };

  return (
    <div ref={setNodeRef} {...listeners} {...attributes} style={style} className="signature-stamp">
      Drag signature here
    </div>
  );
}

export default function SignatureOverlay({ value, onChange }) {
  const containerRef = useRef(null);
  const [position, setPosition] = useState(value);

  useEffect(() => {
    setPosition(value);
  }, [value]);

  const handleDragEnd = ({ delta }) => {
    const rect = containerRef.current?.getBoundingClientRect();
    if (!rect) {
      return;
    }
    const next = {
      x: Math.max(0, Math.min(90, position.x + (delta.x / rect.width) * 100)),
      y: Math.max(0, Math.min(90, position.y + (delta.y / rect.height) * 100))
    };
    setPosition(next);
    onChange(next);
  };

  return (
    <DndContext onDragEnd={handleDragEnd}>
      <div className="preview-frame" ref={containerRef}>
        <SignatureStamp position={position} />
      </div>
    </DndContext>
  );
}
